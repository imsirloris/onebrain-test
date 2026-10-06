package com.loris.onebrain.coupon.infrastructure.web;

import java.util.UUID;

import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Coupon", description = "Create, query and soft delete discount coupons")
@RequestMapping("/coupon")
public interface CouponApi {

    String PROBLEM_JSON = "application/problem+json";

    @Operation(summary = "Create a coupon",
            description = "Special characters are removed from the code, which must keep exactly 6 alphanumeric characters. "
                    + "Discount value must be at least 0.5 and the expiration date cannot be in the past.")
    @ApiResponse(responseCode = "201", description = "Coupon created",
            content = @Content(schema = @Schema(implementation = CouponResponse.class)))
    @ApiResponse(responseCode = "400", description = "Malformed request body",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(value = """
                            {"type":"about:blank","title":"Bad Request","status":400,
                             "detail":"Request body is malformed or has invalid field types.","instance":"/coupon",
                             "code":"MALFORMED_REQUEST"}""")))
    @ApiResponse(responseCode = "422", description = "Business rule violated",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(value = """
                            {"type":"about:blank","title":"Unprocessable Content","status":422,
                             "detail":"Coupon code must contain exactly 6 alphanumeric characters after removing special characters.",
                             "instance":"/coupon","code":"COUPON_CODE_INVALID_LENGTH"}""")))
    @PostMapping
    ResponseEntity<CouponResponse> create(@RequestBody CreateCouponRequest request);

    @Operation(summary = "Get a coupon", description = "Deleted coupons are returned with status DELETED.")
    @ApiResponse(responseCode = "200", description = "Coupon found",
            content = @Content(schema = @Schema(implementation = CouponResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid id format",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Coupon not found",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(value = """
                            {"type":"about:blank","title":"Not Found","status":404,"detail":"Coupon not found.",
                             "instance":"/coupon/df7ddff3-03f5-4362-9bf5-5a5a8ce47b93","code":"COUPON_NOT_FOUND"}""")))
    @GetMapping("/{id}")
    ResponseEntity<CouponResponse> get(
            @Parameter(description = "Coupon id (UUID). Ex.: df7ddff3-03f5-4362-9bf5-5a5a8ce47b93")
            @PathVariable("id") UUID id);

    @Operation(summary = "Delete a coupon",
            description = "Soft delete: the coupon keeps all its data and is marked as DELETED. "
                    + "A coupon can be deleted at any time, but only once.")
    @ApiResponse(responseCode = "204", description = "Coupon deleted")
    @ApiResponse(responseCode = "400", description = "Invalid id format",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Coupon not found",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Coupon already deleted or modified concurrently",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(value = """
                            {"type":"about:blank","title":"Conflict","status":409,"detail":"Coupon is already deleted.",
                             "instance":"/coupon/df7ddff3-03f5-4362-9bf5-5a5a8ce47b93","code":"COUPON_ALREADY_DELETED"}""")))
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
            @Parameter(description = "Coupon id (UUID). Ex.: df7ddff3-03f5-4362-9bf5-5a5a8ce47b93")
            @PathVariable("id") UUID id);
}
