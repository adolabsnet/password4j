/*
 *  (C) Copyright 2020 Password4j (http://password4j.com/).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.password4j;

import javax.crypto.SecretKey;
import java.util.Base64;

public class CompressedPBKDF2Function extends PBKDF2Function
{



    public CompressedPBKDF2Function(Algorithm fromCode, int iterations, int length)
    {
        super(fromCode, iterations, length);
    }

    public CompressedPBKDF2Function(String algorithm, int iterations, int length)
    {
        super(algorithm, iterations, length);
    }

    public static CompressedPBKDF2Function getInstanceFromHash(String hashed)
    {
        String[] parts = hashed.split("\\$");
        if (parts.length == 5)
        {
            int algorithm = Integer.parseInt(parts[1]);
            long configuration = Long.parseLong(parts[2]);

            int iterations = (int) (configuration >> 32);
            int length = (int) configuration;

            return new CompressedPBKDF2Function(Algorithm.fromCode(algorithm), iterations, length);
        }
        throw new BadParametersException("`" + hashed + "` is not a valid hash");
    }


    @Override
    protected String getHash(SecretKey key, String salt)
    {
        String params = Long.toString((((long) getIterations()) << 32) | (getLength() & 0xffffffffL));
        String salt64 = Base64.getEncoder().encodeToString(salt.getBytes());
        String hash64 = super.getHash(key, salt);
        return "$" + getAlgorithm().getCode() + "$" + params + "$" + salt64 + "$" + hash64;
    }

    @Override
    public boolean check(String password, String hashed)
    {
        String salt = getSaltFromHash(hashed);

        Hash internalHas = hash(password, salt);

        return slowEquals(internalHas.getResult().getBytes(), hashed.getBytes());
    }

    @Override
    public boolean check(String plain, String hashed, String salt)
    {
        String realSalt = getSaltFromHash(hashed);
        Hash internalHas = hash(plain, realSalt);
        return slowEquals(internalHas.getResult().getBytes(), hashed.getBytes());
    }

    private String getSaltFromHash(String hashed)
    {
        String[] parts = hashed.split("\\$");
        if (parts.length == 5)
        {
            return new String(Base64.getDecoder().decode(parts[3].getBytes()));
        }
        throw new BadParametersException("`" + hashed + "` is not a valid hash");
    }
}
