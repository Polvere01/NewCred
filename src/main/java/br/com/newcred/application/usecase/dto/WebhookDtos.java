package br.com.newcred.application.usecase.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public class WebhookDtos {
    private WebhookDtos() {}

    public record WebhookPayloadDTO(
            String object,
            List<EntryDTO> entry
    ) {}

    public record EntryDTO(
            String id,
            List<ChangeDTO> changes
    ) {}

    public record ChangeDTO(
            String field,
            ValueDTO value
    ) {}

    public record ValueDTO(
            String messaging_product,
            MetadataDTO metadata,
            List<ContactDTO> contacts,
            List<MessageDTO> messages
    ) {}

    public record MetadataDTO(
            String display_phone_number,
            String phone_number_id
    ) {}

    public record ContactDTO(
            String wa_id,
            ProfileDTO profile
    ) {}

    public record ProfileDTO(
            String name
    ) {}

    public record MessageDTO(
            String from,
            String id,
            String timestamp,  // vem string no payload
            String type,
            TextDTO text
    ) {}

    public record TextDTO(
            String body
    ) {}
}
