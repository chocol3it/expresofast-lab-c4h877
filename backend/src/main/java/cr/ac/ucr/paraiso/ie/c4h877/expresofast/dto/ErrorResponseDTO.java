package cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {

    private final String title;
    private final int status;
    private final String detail;
    private final String instance;
    private final LocalDateTime timestamp;
    private final Map<String, String> invalidFields;

    public ErrorResponseDTO(String title, int status, String detail, String instance) {
        this(title, status, detail, instance, null);
    }

    public ErrorResponseDTO(String title, int status, String detail, String instance,
            Map<String, String> invalidFields) {
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
        this.timestamp = LocalDateTime.now();
        this.invalidFields = invalidFields;
    }

    public String getTitle() {
        return title;
    }

    public int getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }

    public String getInstance() {
        return instance;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getInvalidFields() {
        return invalidFields;
    }
}
