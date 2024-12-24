package org.url.urlshortenerbe.utils;

import org.springframework.stereotype.Component;

@Component
public class Base62Encoder {
    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public String encode(long number) {
        StringBuilder result = new StringBuilder();

        if (number == 0) {
            return "0";
        }

        while (number > 0) {
            int remainder = (int) (number % 62);
            result.append(BASE62.charAt(remainder));
            number /= 62;
        }

        return result.reverse().toString();
    }
}
