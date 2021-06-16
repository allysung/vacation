package com.storm.vacation.annuals;

import com.storm.vacation.accounts.Account;
import com.storm.vacation.accounts.CurrentUser;
import com.storm.vacation.common.ErrorsResource;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * 연차 상세 Controller
 */
@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/annuals/details", produces = MediaTypes.HAL_JSON_VALUE)
public class AnnualDetailController {

    private static final String UPDATE_ANNUALS = "update-annuals";

    private static final String CREATE_ANNUALS = "create-annuals";
    private final AnnualCancelValidator annualCancelValidator;

    private final AnnualService annualService;

    private final ModelMapper modelMapper;

    /**
     * 연차 상세 조회
     *
     * @param id
     * @param currentUser
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity getAnnualDetail(@PathVariable Long id,
                                          @CurrentUser Account currentUser) {

        Optional<AnnualDetail> optionalAnnualDetail = annualService.getAnnualDetail(id);
        if (optionalAnnualDetail.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AnnualDetail annualDetail = optionalAnnualDetail.get();
        AnnualDetailDto annualDetailDto = modelMapper.map(annualDetail, AnnualDetailDto.class);

        EntityModel<AnnualDetailDto> eventResource = AnnualResource.buildResource(annualDetailDto);

        if (annualDetail.getAccount().equals(currentUser)) {
            eventResource.add(linkTo(AnnualDetailController.class).slash(annualDetail.getId()).withRel(UPDATE_ANNUALS));
        }

        return ResponseEntity.ok(eventResource);
    }

    /**
     * 연차 취소 요청
     *
     * @param id
     * @param requestAnnualDto
     * @param errors
     * @param currentUser
     * @return
     */
    @PatchMapping("/{id}")
    public ResponseEntity cancelAnnualDetail(@PathVariable Long id,
                                             @RequestBody @Validated(AnnualDetailDto.CancelVal.class) AnnualDetailDto requestAnnualDto,
                                             Errors errors,
                                             @CurrentUser Account currentUser) {
        Optional<AnnualDetail> optionalAnnualDetail = annualService.getAnnualDetail(id);
        if (optionalAnnualDetail.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AnnualDetail annualDetail = optionalAnnualDetail.get();
        if (annualDetail.getAccount().equals(currentUser) == false) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        annualCancelValidator.validate(annualDetail, requestAnnualDto, errors);
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        Annual cancelAnnual = annualService.cancel(annualDetail);
        AnnualDto annualDto = modelMapper.map(cancelAnnual, AnnualDto.class);

        EntityModel<AnnualDto> annualResource = AnnualResource.buildResource(annualDto);

        return ResponseEntity.ok(annualResource);
    }

    /**
     * 연차 목록 조회
     *
     * @param pageable
     * @param assembler
     * @param account
     * @return
     */
    @GetMapping
    public ResponseEntity queryAnnualDetails(Pageable pageable,
                                             PagedResourcesAssembler<AnnualDetailDto> assembler,
                                             @CurrentUser Account account) {

        Page<AnnualDetailDto> page = annualService.getAnnualDetails(pageable)
                .map(annualDetail -> modelMapper.map(annualDetail, AnnualDetailDto.class));

        PagedModel<EntityModel<AnnualDetailDto>> pagedResources = assembler.toModel(page, AnnualResource::buildResource);
        if (account != null) {
            pagedResources.add(linkTo(AnnualController.class).withRel(CREATE_ANNUALS));
        }
        return ResponseEntity.ok(pagedResources);
    }

    private ResponseEntity badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(ErrorsResource.buildErrorsResource(errors));
    }
}
