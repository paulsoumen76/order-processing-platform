package com.orderprocesssing.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.ChatModel;
import com.openai.models.responses.*;
import com.orderprocesssing.ai.model.AiChatResponse;
import com.orderprocesssing.ai.model.AiUiResponse;
import com.orderprocesssing.ai.model.ProductSearchArguments;
import com.orderprocesssing.ai.model.TopProductsArguments;
import com.orderprocesssing.ai.tool.ProductSearchTool;
import com.orderprocesssing.ai.tool.TopProductsTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AiAssistService {

    private final OpenAIClient openAIClient;
    private final ProductSearchTool productSearchTool;
    private final ObjectMapper objectMapper;
    private final TopProductsTool topProductsTool;
    private static final Logger log =
            LoggerFactory.getLogger(AiAssistService.class);
    public AiAssistService(
            OpenAIClient openAIClient,
            ProductSearchTool productSearchTool,
            ObjectMapper objectMapper, TopProductsTool topProductsTool) {

        this.openAIClient = openAIClient;
        this.productSearchTool = productSearchTool;
        this.objectMapper = objectMapper;
        this.topProductsTool = topProductsTool;
    }

    public AiChatResponse chat(String message) {

        ResponseCreateParams params =
                ResponseCreateParams.builder()
                        .model(ChatModel.GPT_5_2)
                        .input("""
                            You are the AI assistant for an order processing platform.

                            You can answer general conversational questions
                            without tools.

                            When the user asks about products, orders,
                            inventory, payments, or other business data,
                            use the appropriate available tool.

                                After receiving a tool result, generate the final answer as JSON
                                matching this structure:
                               \s
                                {
                                  "message": "Human readable response",
                                  "ui": {
                                    "type": "TEXT | PRODUCT_LIST | PRODUCT_DETAILS | TABLE",
                                    "data": null
                                  }
                                }
                               \s
                                UI rules:
                               \s
                                - Use TEXT for normal conversational responses.
                                - Use PRODUCT_LIST when the user asks to search, find, show, or list multiple products.
                                - Use PRODUCT_DETAILS when the user asks for details about one specific product,
                                  including requests such as:
                                  "show me the second product",
                                  "give me details of this product",
                                  "tell me more about this product",
                                  "show the second top-ordered product with details".
                                - Use TABLE when displaying multiple records in a tabular format.
                                - Never put image URLs, signed URLs, or other raw URLs inside "message".
                                - If an image URL is available, put it only inside "ui.data".
                                - Never put product data in "message" when that data can be represented by the UI.
                                - The "message" should be a short human-readable explanation.
                                - Never invent product data.
                                - Use only data returned by the tools.
                                - Return valid JSON only.

                            User message:
                           \s""" + message)
                        .addTool(searchProductsTool())
                        .addTool(topProductsTool())
                        .build();

        Response response = openAIClient
                .responses()
                .create(params);

        for (var item : response.output()) {

            if (item.isFunctionCall()) {

                ResponseFunctionToolCall functionCall =
                        item.asFunctionCall();

                log.info(
                        "LLM requested tool: {}, arguments: {}",
                        functionCall.name(),
                        functionCall.arguments()
                );

                String toolResult = executeTool(functionCall);

                log.info(
                        "Tool execution completed: {}, resultLength={}",
                        functionCall.name(),
                        toolResult.length()
                );

                ResponseInputItem.FunctionCallOutput functionCallOutput =
                        ResponseInputItem.FunctionCallOutput.builder()
                                .callId(functionCall.callId())
                                .output(toolResult)
                                .build();

                ResponseInputItem outputItem =
                        ResponseInputItem.ofFunctionCallOutput(
                                functionCallOutput
                        );

                ResponseCreateParams secondParams =
                        ResponseCreateParams.builder()
                                .model(ChatModel.GPT_5_2)
                                .previousResponseId(response.id())
                                .inputOfResponse(List.of(outputItem))
                                .build();

                Response secondResponse =
                        openAIClient
                                .responses()
                                .create(secondParams);

                return extractAiResponse(secondResponse);
            }
        }

        return extractAiResponse(response);
    }

    private FunctionTool searchProductsTool() {

        return FunctionTool.builder()
                .name("searchProducts")
                .description(
                        "Search products in the inventory by product name or search phrase."
                )
                .parameters(
                        FunctionTool.Parameters.builder()
                                .putAdditionalProperty(
                                        "type",
                                        JsonValue.from("object")
                                )
                                .putAdditionalProperty(
                                        "properties",
                                        JsonValue.from(
                                                Map.of(
                                                        "search",
                                                        Map.of(
                                                                "type", "string",
                                                                "description",
                                                                "Product name or search phrase to search for"
                                                        )
                                                )
                                        )
                                )
                                .putAdditionalProperty(
                                        "required",
                                        JsonValue.from(List.of("search"))
                                )
                                .putAdditionalProperty(
                                        "additionalProperties",
                                        JsonValue.from(false)
                                )
                                .build()
                )
                .strict(true)
                .build();
    }

    private String executeTool(ResponseFunctionToolCall functionCall) {

        try {

            switch (functionCall.name()) {

                case "searchProducts":

                    ProductSearchArguments searchArguments =
                            objectMapper.readValue(
                                    functionCall.arguments(),
                                    ProductSearchArguments.class
                            );

                    return productSearchTool.searchProducts(
                            searchArguments.search()
                    );

                case "getTopOrderedProducts":

                    TopProductsArguments topArguments =
                            objectMapper.readValue(
                                    functionCall.arguments(),
                                    TopProductsArguments.class
                            );

                    return topProductsTool.getTopOrderedProducts(
                            topArguments.limit()
                    );

                default:
                    throw new IllegalArgumentException(
                            "Unknown tool: " + functionCall.name()
                    );
            }

        } catch (JsonProcessingException e) {

            throw new IllegalArgumentException(
                    "Invalid arguments from LLM for tool: "
                            + functionCall.name(),
                    e
            );
        }
    }

    private FunctionTool topProductsTool() {

        return FunctionTool.builder()
                .name("getTopOrderedProducts")
                .description(
                        "Get the products that have been ordered the most. " +
                                "Use this when the user asks which products are most ordered, " +
                                "best selling, or top ordered products."
                )
                .parameters(
                        FunctionTool.Parameters.builder()
                                .putAdditionalProperty(
                                        "type",
                                        JsonValue.from("object")
                                )
                                .putAdditionalProperty(
                                        "properties",
                                        JsonValue.from(
                                                Map.of(
                                                        "limit",
                                                        Map.of(
                                                                "type", "integer",
                                                                "description",
                                                                "Maximum number of top products to return"
                                                        )
                                                )
                                        )
                                )
                                .putAdditionalProperty(
                                        "required",
                                        JsonValue.from(List.of("limit"))
                                )
                                .putAdditionalProperty(
                                        "additionalProperties",
                                        JsonValue.from(false)
                                )
                                .build()
                )
                .strict(true)
                .build();
    }

    private AiChatResponse extractAiResponse(Response response) {

        String text = response.output()
                .stream()
                .flatMap(item -> item.message().stream())
                .flatMap(messageItem -> messageItem.content().stream())
                .flatMap(content -> content.outputText().stream())
                .map(ResponseOutputText::text)
                .findFirst()
                .orElse("""
                    {
                      "message": "I couldn't generate a response.",
                      "ui": {
                        "type": "TEXT",
                        "data": null
                      }
                    }
                    """);

        try {
            return objectMapper.readValue(
                    text,
                    AiChatResponse.class
            );
        } catch (JsonProcessingException e) {

            log.error("Failed to parse AI structured response: {}", text, e);

            return new AiChatResponse(
                    text,
                    new AiUiResponse("TEXT", null)
            );
        }
    }
}