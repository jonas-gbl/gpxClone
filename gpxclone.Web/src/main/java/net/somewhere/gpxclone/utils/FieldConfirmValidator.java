/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.somewhere.gpxclone.utils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;


/**
 *
 * @author Jonas
 */
public class FieldConfirmValidator implements ConstraintValidator<FieldConfirm, Object>{

    private String candidateFieldName;
    private String confirmFieldName;
    private String message;
    
    @Override
    public void initialize(FieldConfirm constraintAnnotation) {
        candidateFieldName = constraintAnnotation.field();
        confirmFieldName = constraintAnnotation.confirmation();
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(final Object someObject, final ConstraintValidatorContext constraintContext)
    {
        
        boolean isValid=false;
        
        try
        {
            BeanWrapper someObjectWrapper = new BeanWrapperImpl(someObject);//final Object candidateField = BeanUtils.getProperty(someObject, candidateFieldName);
            final Object candidateField = someObjectWrapper.getPropertyValue(candidateFieldName);
            final Object confirmField = someObjectWrapper.getPropertyValue(confirmFieldName);
            
            isValid = candidateField == null && confirmField == null || candidateField != null && candidateField.equals(confirmField);
            
            if(!isValid)
            {
                constraintContext.disableDefaultConstraintViolation();
                constraintContext.
                        buildConstraintViolationWithTemplate(message).
                            addNode(candidateFieldName).
                                addConstraintViolation();
            }
        
            
            return isValid;
        }
        catch(Exception e){}
        
        return true;
    }
    
}
