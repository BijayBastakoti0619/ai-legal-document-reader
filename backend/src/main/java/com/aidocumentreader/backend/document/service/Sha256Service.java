package com.aidocumentreader.backend.document.service;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class Sha256Service {

    public String calculate(byte[] fileBytes) {

        if (fileBytes == null) {
            throw new IllegalArgumentException("File bytes must not be null.");
        }

        try {
            MessageDigest messageDigest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = messageDigest.digest(fileBytes);

            return HexFormat.of().formatHex(hashBytes);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available.",
                    exception
            );
        }
    }
}
