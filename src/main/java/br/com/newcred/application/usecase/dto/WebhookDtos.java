package br.com.newcred.application.usecase.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public class WebhookDtos {
    private WebhookDtos() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WebhookPayloadDTO(String object, java.util.List<EntryDTO> entry) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EntryDTO(String id, java.util.List<ChangeDTO> changes) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChangeDTO(String field, ValueDTO value) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ValueDTO(
            List<ContactDTO> contacts,
            List<MessageDTO> messages,
            MetadataDTO metadata,
            List<StatusDTO> statuses
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MetadataDTO(String phone_number_id, String display_phone_number) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContactDTO(String wa_id, ProfileDTO profile) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProfileDTO(String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MessageDTO(
            String from,
            String id,
            String timestamp,
            TextDTO text,
            String type,
            AudioDTO audio,
            ImageDto image,
            DocumentDTO document,
            VideoDto video,
            ButtonDTO button,
            InteractiveDTO interactive

    ) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TextDTO(String body) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AudioDTO(String id, String myne_type) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ImageDto(String id, String mime_type) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DocumentDTO(String id, String filename, String mime_type) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VideoDto(String id, String filename, String mime_type) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ButtonDTO(String text, String payload) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InteractiveDTO(
            String type,
            ButtonReplyDTO button_reply,
            ListReplyDTO list_reply
    ) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ButtonReplyDTO(String id, String title) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ListReplyDTO(String id, String title, String description) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StatusDTO(
            String id,
            String status,
            String timestamp,
            String recipient_id
    ) {}
}
