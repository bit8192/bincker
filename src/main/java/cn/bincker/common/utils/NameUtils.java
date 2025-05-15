package cn.bincker.common.utils;

public class NameUtils {
    public static String linkCaseToCamelCase(String kebabCase, char separator) {
        StringBuilder camelCase = new StringBuilder();
        boolean nextUpper = false;

        for (int i = 0; i < kebabCase.length(); i++) {
            char currentChar = kebabCase.charAt(i);

            if (currentChar == separator) {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    camelCase.append(Character.toUpperCase(currentChar));
                    nextUpper = false;
                } else {
                    camelCase.append(currentChar);
                }
            }
        }

        return camelCase.toString();
    }

    public static String camelCaseToLinkCase(String camelCase, char separator) {
        StringBuilder kebabCase = new StringBuilder();

        for (int i = 0; i < camelCase.length(); i++) {
            var currentChar = camelCase.charAt(i);
            var nextCharIsUpper = i+1 < camelCase.length() && Character.isUpperCase(camelCase.charAt(i+1));
            if (Character.isUpperCase(currentChar)) {
                if (!nextCharIsUpper) {
                    kebabCase.append(separator);
                }
                kebabCase.append(Character.toLowerCase(currentChar));
            }else{
                kebabCase.append(currentChar);
            }
        }

        return kebabCase.toString();
    }

    public static String kebabCaseToCamelCase(String kebabCase) {
        return linkCaseToCamelCase(kebabCase, '-');
    }

    public static String camelCaseToKebabCase(String camelCase) {
        return camelCaseToLinkCase(camelCase, '-');
    }

    public static String snakeCaseToCamelCase(String kebabCase) {
        return linkCaseToCamelCase(kebabCase, '_');
    }

    public static String camelCaseToSnakeCase(String camelCase) {
        return camelCaseToLinkCase(camelCase, '_');
    }
}
