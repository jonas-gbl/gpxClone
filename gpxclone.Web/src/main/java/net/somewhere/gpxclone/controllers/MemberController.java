/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.somewhere.gpxclone.controllers;


import javax.validation.Valid;
import net.somewhere.gpxclone.services.UserAlreadyExistsException;
import net.somewhere.gpxclone.services.UserService;
import net.somewhere.gpxclone.utils.memberForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

/**
 *
 * @author Jonas
 */
@Controller
//@SessionAttributes(types = User.class)
@SessionAttributes(types = memberForm.class)
public class MemberController {
    
    private final UserService userService;

    @Autowired
    public MemberController(UserService userService)
    {
        this.userService = userService;
    }

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("user.pId");
    }

    @RequestMapping(value = "/members/new", method = RequestMethod.GET)
    public String initCreationForm(Model model) {
        //User user = new User();
        //model.addAttribute(user);
        memberForm userForm = new memberForm();
        model.addAttribute(userForm);
        return "members/bt_memberForm";
    }
    
    @RequestMapping(value = "/members/new", method = RequestMethod.POST)
    //public String processCreationForm(@Valid User user, BindingResult result, SessionStatus status) {
    public String processCreationForm(@Valid memberForm userForm, BindingResult result, SessionStatus status,Model model) {
        if (result.hasErrors()) {
            return "members/bt_memberForm";
        } else {
            try
            {
                this.userService.createUser(userForm.getUser());
                status.setComplete();
            }
            catch(UserAlreadyExistsException ex)
            {
                model.addAttribute("problem","User already exists.");
                return "errors/bt_oops";
            }
            return "members/bt_memberCreated";
        }
    }
    
    @RequestMapping(value = "/members/activate", method = RequestMethod.GET)
    public String activateMember(@RequestParam("key") String key,Model model){
    
        if(this.userService.activateUser(key)!=null)
        {
            return "redirect:/";
        }
        else
        {
            model.addAttribute("problem","Member account could not be activated.");
            return "errors/bt_oops";
        }
    }
    
    @RequestMapping(value = "/members/login", method = RequestMethod.GET)
    public String initLoginForm(Model model) {
        return "members/bt_memberLogin";
    }
    
 
    @RequestMapping(value = "/members/error", method = RequestMethod.GET)
    public String initSecurityError(Model model) {
        model.addAttribute("problem","There was an Authentication/Authorization error.");
        return "errors/bt_oops";
    }  
}
