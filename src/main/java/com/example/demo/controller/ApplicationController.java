package com.example.demo.controller;

import com.example.demo.model.Receipt;
import com.example.demo.model.User;
import com.example.demo.service.ReceiptService;
import com.example.demo.service.UserService;
import com.example.demo.service.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class ApplicationController {
    private final ReceiptService receiptService;
    private final UserService userService;
    private final SecurityService securityService;
    private final UserValidator userValidator;

    @Autowired
    public ApplicationController(ReceiptService receiptService, UserService userService, SecurityService securityService, UserValidator userValidator) {
        this.receiptService = receiptService;
        this.userService = userService;
        this.securityService = securityService;
        this.userValidator = userValidator;
    }

    /***
     * @param model supply attributes used for rendering views
     * @param principal is the currently logged in user
     * @return what view to use
     */
    @GetMapping("/receipts")
    public String showTable(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());

        System.out.println("THE CURRENT USER " + principal.getName());
        List<Receipt> receipts = receiptService.getReceiptsByUser(user);

        model.addAttribute("receipts", receipts);
        return "receipts";
    }

    @GetMapping("/registration")
    public String registration(Model model) {
        if (securityService.isAuthenticated()) {
            return "redirect:/";
        }

        model.addAttribute("userForm", new User());

        return "registration";
    }

    @PostMapping("/registration")
    public String registration(@ModelAttribute("userForm") User userForm, BindingResult bindingResult) {
        userValidator.validate(userForm, bindingResult);

        if (bindingResult.hasErrors()) {
            return "registration";
        }

        userService.save(userForm);

        securityService.autoLogin(userForm.getUsername(), userForm.getPasswordConfirm());

        return "redirect:/welcome";
    }

    @GetMapping("/login")
    public String login(Model model, String error, String logout) {
        if (securityService.isAuthenticated()) {
            return "redirect:/";
        }

        if (error != null)
            model.addAttribute("error", "Your username and password is invalid.");

        if (logout != null)
            model.addAttribute("message", "You have been logged out successfully.");

        return "login";
    }

    @GetMapping({"/", "/welcome"})
    public String welcome(Model model) {
        return "welcome";
    }
}
