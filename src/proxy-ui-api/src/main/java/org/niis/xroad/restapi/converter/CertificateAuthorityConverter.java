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
package org.niis.xroad.restapi.converter;

import ee.ria.xroad.common.conf.globalconf.ApprovedCAInfo;

import com.google.common.collect.Streams;
import org.niis.xroad.restapi.openapi.model.CertificateAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Converter for CertificateAuthority related data between openapi and service domain classes
 */
@Component
public class CertificateAuthorityConverter {

    /**
     * convert ApprovedCAInfo into openapi CertificateAuthority class
     * @param approvedCAInfo
     * @return
     */
    public CertificateAuthority convert(ApprovedCAInfo approvedCAInfo) {
        CertificateAuthority ca = new CertificateAuthority();
        ca.setName(approvedCAInfo.getName());
        ca.setAuthenticationOnly(Boolean.TRUE.equals(approvedCAInfo.getAuthenticationOnly()));
        return ca;
    }

    /**
     * convert a group of ApprovedCAInfos into a list of CertificateAuthorities
     * @param approvedCAInfos
     * @return
     */
    public List<CertificateAuthority> convert(Iterable<ApprovedCAInfo> approvedCAInfos) {
        return Streams.stream(approvedCAInfos)
                .map(this::convert)
                .collect(Collectors.toList());
    }
}
