package br.com.newcred.application.usecase.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public class WebhookDtos {
    private WebhookDtos() {}

    public record WebhookPayloadDTO(String object, java.util.List<EntryDTO> entry) {}
    public record EntryDTO(String id, java.util.List<ChangeDTO> changes) {}
    public record ChangeDTO(String field, ValueDTO value) {}

    public record ValueDTO(
            List<ContactDTO> contacts,
            List<MessageDTO> messages,
            MetadataDTO metadata,
            List<StatusDTO> statuses
    ) {}

    public record MetadataDTO(String phone_number_id, String display_phone_number) {}

    public record ContactDTO(String wa_id, ProfileDTO profile) {}
    public record ProfileDTO(String name) {}

    public record MessageDTO(
            String from,
            String id,
            String timestamp,
            TextDTO text,
            String type,
            AudioDTO audio,
            ImageDto image,
            DocumentDTO document


    ) {}
    public record TextDTO(String body) {}
    public record AudioDTO(String id, String myne_type) {}
    public record ImageDto(String id, String mime_type) {}
    public record DocumentDTO(String id, String filename, String mime_type) {}

    public record StatusDTO(
            String id,
            String status,
            String timestamp,
            String recipient_id
    ) {}
}
