package com.storm.vacation.annuals;

import com.storm.vacation.accounts.Account;
import com.storm.vacation.accounts.CurrentUser;
import com.storm.vacation.common.ErrorsResource;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * 연차 Controller
 */
@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/annuals", produces = MediaTypes.HAL_JSON_VALUE)
public class AnnualController {

    private static final String QUERY_ANNUALS = "query-annuals";

    private static final String UPDATE_ANNUALS = "update-annuals";

    private final ModelMapper modelMapper;

    private final AnnualValidator annualValidator;

    private final AnnualService annualService;

    /**
     * 사용자 자신의 연차 조회
     *
     * @param currentUser
     * @return
     */
    @GetMapping("/me")
    public ResponseEntity getMyAnnual(@CurrentUser Account currentUser) {

        if (Objects.isNull(currentUser)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Annual annual = annualService.findOrCreateNew(currentUser);

        AnnualDto annualDto = modelMapper.map(annual, AnnualDto.class);

        EntityModel<AnnualDto> entityModel = EntityModel.of(annualDto);
        entityModel.add(linkTo(AnnualController.class).slash("me").withSelfRel());

        if (annual.getAccount().equals(currentUser)) {
            entityModel.add(linkTo(AnnualController.class).slash(annual.getId()).withRel(UPDATE_ANNUALS));
        }

        return ResponseEntity.ok(entityModel);
    }

    /**
     * 연차 요청
     *
     * @param requestAnnualDto
     * @param currentUser
     * @param errors
     * @return
     */
    @PostMapping
    public ResponseEntity createAnnual(@RequestBody @Validated AnnualDetailDto requestAnnualDto,
                                       @CurrentUser Account currentUser, Errors errors) {
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        Annual currentUserAnnual = annualService.findOrCreateNew(currentUser);
        annualValidator.validate(currentUserAnnual, requestAnnualDto, errors);
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        Annual annual = annualService.requestVacation(currentUserAnnual, requestAnnualDto);

        WebMvcLinkBuilder selfLinkBuilder = linkTo(AnnualController.class).slash(annual.getId());
        URI createdUri = selfLinkBuilder.toUri();

        AnnualDto annualDto = modelMapper.map(annual, AnnualDto.class);

        EntityModel<AnnualDto> annualResource = AnnualResource.buildResource(annualDto);
        annualResource.add(linkTo(AnnualController.class).withRel(QUERY_ANNUALS));
        annualResource.add(selfLinkBuilder.withRel(UPDATE_ANNUALS));

        return ResponseEntity.created(createdUri).body(annualResource);
    }

    /**
     * 연차 조회
     *
     * @param id
     * @param currentUser
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity getAnnual(@PathVariable Long id,
                                    @CurrentUser Account currentUser) {

        Optional<Annual> optionalAnnual = annualService.getAnnual(id);
        if (optionalAnnual.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Annual annual = optionalAnnual.get();
        AnnualDto annualDto = modelMapper.map(annual, AnnualDto.class);

        EntityModel<AnnualDto> eventResource = AnnualResource.buildResource(annualDto);

        if (annual.getAccount().equals(currentUser)) {
            eventResource.add(linkTo(AnnualController.class).slash(annual.getId()).withRel(UPDATE_ANNUALS));
        }

        return ResponseEntity.ok(eventResource);
    }

    private ResponseEntity badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(ErrorsResource.buildErrorsResource(errors));
    }
}
