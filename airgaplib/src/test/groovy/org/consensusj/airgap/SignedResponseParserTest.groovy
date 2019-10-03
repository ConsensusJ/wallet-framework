/*
 * Copyright by the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.consensusj.airgap


import org.consensusj.airgap.json.TransactionSignatureResponse
import spock.lang.Specification

/**
 *
 */
class SignedResponseParserTest extends Specification {
    def "Parse"() {
        given:
        def parser = new SignedResponseParser();

        when:
        TransactionSignatureResponse response = parser.parse(responseJsonString)

        then:
        response != null
        response.header.version == 1
        response.header.format == 'AirgappedSigning'
        response.transaction.asset == AirGapProtocol.AssetType.BTCT.toString()
        response.transaction.inputSignatures[0].uid == '0C06DCB5-17AB-4E3A-B6A7-2F8808933F17'
        response.transaction.inputSignatures[0].ecPublicKey == 'AoeGQTRvbM+k7QpQ8Xhr+9GJH/IAtMBABAqASrwsWtaa'
        response.transaction.inputSignatures[0].ecSignature == 'MEUCIQDi+KSPdIIVlfFP2c8AmYqu3y3Fa+Y2Mm73Z37k1Or0HAIgKTdJLWYx4ohRnrGcj16ICyNr5/OkFVPSPNEC25VubUYB'
    }


    static final responseJsonString = '''
{
  "transaction": {
    "uid": "96460F01-C415-49B4-A8A4-3254D9CBB323",
    "asset": "BTCT",
    "inputSignatures": [
      {
        "uid": "0C06DCB5-17AB-4E3A-B6A7-2F8808933F17",
        "ecPublicKey": "AoeGQTRvbM+k7QpQ8Xhr+9GJH\\/IAtMBABAqASrwsWtaa",
        "ecSignature": "MEUCIQDi+KSPdIIVlfFP2c8AmYqu3y3Fa+Y2Mm73Z37k1Or0HAIgKTdJLWYx4ohRnrGcj16ICyNr5\\/OkFVPSPNEC25VubUYB"
      }
    ]
  },
  "header": {
    "version": 1,
    "format": "AirgappedSigning"
  }
}
'''
}
