package com.webaudit.exception;

import com.webaudit.constants.ErrorConstants;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String resourceName, String id) {
        super(String.format("%s with ID '%s' was not found", resourceName, id),
              HttpStatus.NOT_FOUND,
              ErrorConstants.ERR_RESOURCE_NOT_FOUND);
    }
}
