package generated.org.springframework.validators;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Digits;
import org.hibernate.validator.internal.constraintvalidators.bv.DigitsValidatorForCharSequence;
import org.jacodb.approximation.annotation.Approximate;
import org.usvm.api.Engine;
import org.usvm.spring.api.SpringEngine;


@Approximate(DigitsValidatorForCharSequence.class)
public class DigitsValidatorForCharSequenceImpl {

    private int maxIntegerLength;
    private int maxFractionLength;

    public void initialize(Digits constraintAnnotation) {
        this.maxIntegerLength = constraintAnnotation.integer();
        this.maxFractionLength = constraintAnnotation.fraction();
    }

    public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
        return SpringEngine.isInsideDatabase() ?
                isValidInsideDatabase(charSequence)
                : isValidWeb(charSequence);
    }

    private boolean isValidInsideDatabase(CharSequence charSequence) {

        int len = charSequence.length();
        int targetLen = maxIntegerLength + maxFractionLength + (maxFractionLength == 0 ? 0 : 1);

        if (len == targetLen) SpringEngine.markAsGoodPath();
        else SpringEngine.markAsBadPath();

        Engine.assume(len <= targetLen);

        for (int i = 0; i < len; i++) {
            char c = charSequence.charAt(i);
            if (i == maxIntegerLength) Engine.assume(c == '.');
            else Engine.assume(Character.isDigit(c));
        }

        return true;
    }

    private boolean isValidWeb(CharSequence charSequence) {

        int len = charSequence.length();
        int targetLen = maxIntegerLength + maxFractionLength + (maxFractionLength == 0 ? 0 : 1);

        if (len > targetLen) {
            SpringEngine.markAsEdgeCasePath();
            return false;
        }

        for (int i = 0; i < len; i++) {
            char c = charSequence.charAt(i);
            if (i == maxIntegerLength && c != '.') {
                SpringEngine.markAsEdgeCasePath();
                return false;
            }

            if (!Character.isDigit(c)) {
                SpringEngine.markAsEdgeCasePath();
                return false;
            }
        }

        return true;
    }
}
