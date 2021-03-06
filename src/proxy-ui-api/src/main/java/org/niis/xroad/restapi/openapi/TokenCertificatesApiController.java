/**
 * The MIT License
 * Copyright (c) 2018 Estonian Information System Authority (RIA),
 * Nordic Institute for Interoperability Solutions (NIIS), Population Register Centre (VRK)
 * Copyright (c) 2015-2017 Estonian Information System Authority (RIA), Population Register Centre (VRK)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.niis.xroad.restapi.openapi;

import ee.ria.xroad.signer.protocol.dto.CertificateInfo;

import lombok.extern.slf4j.Slf4j;
import org.niis.xroad.restapi.converter.TokenCertificateConverter;
import org.niis.xroad.restapi.openapi.model.TokenCertificate;
import org.niis.xroad.restapi.service.CertificateAlreadyExistsException;
import org.niis.xroad.restapi.service.CertificateNotFoundException;
import org.niis.xroad.restapi.service.ClientNotFoundException;
import org.niis.xroad.restapi.service.GlobalConfService;
import org.niis.xroad.restapi.service.KeyNotFoundException;
import org.niis.xroad.restapi.service.TokenCertificateService;
import org.niis.xroad.restapi.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * certificates api
 */
@Controller
@RequestMapping("/api")
@Slf4j
@PreAuthorize("denyAll")
public class TokenCertificatesApiController implements TokenCertificatesApi {

    private final TokenCertificateService tokenCertificateService;
    private final TokenCertificateConverter tokenCertificateConverter;

    @Autowired
    public TokenCertificatesApiController(TokenCertificateService tokenCertificateService,
            TokenCertificateConverter tokenCertificateConverter) {
        this.tokenCertificateService = tokenCertificateService;
        this.tokenCertificateConverter = tokenCertificateConverter;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('IMPORT_AUTH_CERT', 'IMPORT_SIGN_CERT')")
    public ResponseEntity<TokenCertificate> importCertificate(Resource certificateResource) {
        byte[] certificateBytes = ResourceUtils.springResourceToBytesOrThrowBadRequest(certificateResource);
        CertificateInfo certificate = null;
        try {
            certificate = tokenCertificateService.importCertificate(certificateBytes);
        } catch (GlobalConfService.GlobalConfOutdatedException | ClientNotFoundException | KeyNotFoundException
                | TokenCertificateService.WrongCertificateUsageException
                | TokenCertificateService.InvalidCertificateException
                | TokenCertificateService.AuthCertificateNotSupportedException e) {
            throw new BadRequestException(e);
        } catch (CertificateAlreadyExistsException | TokenCertificateService.CsrNotFoundException e) {
            throw new ConflictException(e);
        }
        TokenCertificate tokenCertificate = tokenCertificateConverter.convert(certificate);
        return ApiUtil.createCreatedResponse("/api/token-certificates/{hash}", tokenCertificate,
                tokenCertificate.getCertificateDetails().getHash());
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_CERT')")
    public ResponseEntity<TokenCertificate> getCertificate(String hash) {
        CertificateInfo certificateInfo;
        try {
            certificateInfo = tokenCertificateService.getCertificateInfo(hash);
        } catch (CertificateNotFoundException e) {
            throw new ResourceNotFoundException(e);
        }

        TokenCertificate tokenCertificate = tokenCertificateConverter.convert(certificateInfo);
        return new ResponseEntity<>(tokenCertificate, HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('IMPORT_AUTH_CERT', 'IMPORT_SIGN_CERT')")
    public ResponseEntity<TokenCertificate> importCertificateFromToken(String hash) {
        CertificateInfo certificate = null;
        try {
            certificate = tokenCertificateService.importCertificateFromToken(hash);
        } catch (GlobalConfService.GlobalConfOutdatedException | ClientNotFoundException | KeyNotFoundException
                | TokenCertificateService.WrongCertificateUsageException
                | TokenCertificateService.InvalidCertificateException
                | TokenCertificateService.AuthCertificateNotSupportedException e) {
            throw new BadRequestException(e);
        } catch (CertificateAlreadyExistsException | TokenCertificateService.CsrNotFoundException e) {
            throw new ConflictException(e);
        } catch (CertificateNotFoundException e) {
            throw new ResourceNotFoundException(e);
        }
        TokenCertificate tokenCertificate = tokenCertificateConverter.convert(certificate);
        return ApiUtil.createCreatedResponse("/api/token-certificates/{hash}", tokenCertificate,
                tokenCertificate.getCertificateDetails().getHash());
    }
}
