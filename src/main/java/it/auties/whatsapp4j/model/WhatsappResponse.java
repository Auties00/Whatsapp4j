package it.auties.whatsapp4j.model;

import com.fasterxml.jackson.core.type.TypeReference;
import it.auties.whatsapp4j.response.model.JsonListResponse;
import it.auties.whatsapp4j.response.model.JsonResponse;
import it.auties.whatsapp4j.response.model.Response;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.util.HashMap;

import static it.auties.whatsapp4j.utils.JsonContext.JACKSON;

/**
 * A model that contains information about a response sent by Whatsapp for a request
 * @param tag         the tag used for the request
 * @param description a nullable String that describes how to categorize the data that is object holds
 * @param data        the data that this object holds
 */
@Builder
public record WhatsappResponse(@NotNull String tag, String description,
                               @NotNull Response data) {

    /**
     * Constructs a new instance of WhatsappResponse from a json string
     *
     * @param parse the json string to parse
     * @return a new instance of WhatsappResponse with the above characteristics
     * @throws IllegalArgumentException if {@code parse} cannot be parsed
     */
    public static @NotNull
    WhatsappResponse fromJson(@NotNull String parse) {
        try {
            var split = parse.split(",", 2);
            if (split.length != 2 && parse.startsWith("!")) {
                return new WhatsappResponse(parse, "pong", new JsonResponse(new HashMap<>()));
            }

            var response = WhatsappResponse.builder().tag(split[0]);
            var content = parseContent(split[1], 0);
            if (content.isEmpty()) {
                return response.data(new JsonResponse(new HashMap<>())).build();
            }

            var jsonNode = JACKSON.readTree(content);
            if (!jsonNode.isArray()) {
                return response.data(new JsonResponse(JACKSON.readerFor(new TypeReference<>() {
                }).readValue(jsonNode))).build();
            }

            var possibleMap = jsonNode.get(1);
            if (possibleMap == null) {
                return response.data(new JsonListResponse(JACKSON.readerFor(new TypeReference<>() {
                }).readValue(jsonNode))).build();
            }

            var possibleMapContent = possibleMap.toString();
            if (!possibleMapContent.startsWith("{") || !possibleMapContent.endsWith("}")) {
                return response.data(new JsonListResponse(JACKSON.readerFor(new TypeReference<>() {
                }).readValue(jsonNode))).build();
            }

            return response.description(jsonNode.get(0).textValue()).data(new JsonResponse(JACKSON.readerFor(new TypeReference<>() {
            }).readValue(possibleMap))).build();
        } catch (IOException ex) {
            throw new IllegalArgumentException("WhatsappAPI: Cannot deserialize %s into a WhatsappResponse".formatted(parse));
        }
    }

    private static @NotNull
    String parseContent(@NotNull String content, int index) {
        return content.length() > index && content.charAt(index) == ',' ? parseContent(content, index + 1) : content.substring(index);
    }
}
