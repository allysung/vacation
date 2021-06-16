package com.storm.vacation.annuals;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class AnnualResource {

    public static EntityModel<AnnualDto> buildResource(AnnualDto annual, Link... links) {

        EntityModel<AnnualDto> entityModel = EntityModel.of(annual, links);
        entityModel.add(linkTo(AnnualController.class).slash(annual.getId()).withSelfRel());
        return entityModel;
    }

    public static EntityModel<AnnualDetailDto> buildResource(AnnualDetailDto annualDetail, Link... links) {
        EntityModel<AnnualDetailDto> entityModel = EntityModel.of(annualDetail, links);
        entityModel.add(linkTo(AnnualDetailController.class).slash(annualDetail.getId()).withSelfRel());
        return entityModel;
    }
}
