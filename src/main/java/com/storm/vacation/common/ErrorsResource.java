package com.storm.vacation.common;

import com.storm.vacation.index.IndexController;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.validation.Errors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ErrorsResource {

    public static EntityModel<Errors> buildErrorsResource(Errors content, Link... links) {
        var errorsEntityModel = EntityModel.of(content, links);
        errorsEntityModel.add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
        return errorsEntityModel;
    }

    public static EntityModel<ErrorContent> buildErrorsResource(ErrorContent content, Link... links) {
        var errorsEntityModel = EntityModel.of(content, links);
        errorsEntityModel.add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
        return errorsEntityModel;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorContent {

        private String code;
        private String message;
    }
}
