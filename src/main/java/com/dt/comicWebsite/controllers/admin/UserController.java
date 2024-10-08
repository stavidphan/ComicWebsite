package com.dt.comicWebsite.controllers.admin;

import com.dt.comicWebsite.models.Role;
import com.dt.comicWebsite.models.User;
import com.dt.comicWebsite.services.RoleService;
import com.dt.comicWebsite.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;

    // READ
    @GetMapping({"", "/"})
    public String showUserList(Model model) {
        List<User> users = userService.getAll();
        model.addAttribute("users", users);
        return "admin/user/listUser";
    }

    // CREATE
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleService.getAll());
        return "admin/user/createUser";
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute @Valid User user, BindingResult bindingResult, @RequestParam("roleIds") List<Integer> roleIds, Model model) {
        // check if username or email already exists
        if (userService.findByUsername(user.getUsername()) != null || userService.findByEmail(user.getEmail()) != null) {
            bindingResult.rejectValue("username", "error.user", "User already exists");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("allRoles", roleService.getAll());
            return "admin/user/createUser";
        }

        Set<Role> roles = new HashSet<>();
        for (Integer roleId : roleIds) {
            Role role = roleService.getById(roleId);
            if (role != null) {
                roles.add(role);
            }
        }
        user.setRoles(roles);
        userService.save(user);
        return "redirect:/admin/user";
    }

    // EDIT
    @GetMapping("/edit")
    public String showEditForm(@RequestParam int id, Model model) {
        User user = userService.getById(id);
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleService.getAll());

        // previously selected roles
        model.addAttribute("selectedRoleIds", user.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toList()));

        return "admin/user/editUser";
    }

    @PostMapping("/edit")
    public String editUser(@RequestParam int id, @ModelAttribute @Valid User user, BindingResult bindingResult, @RequestParam("roleIds") List<Integer> roleIds, Model model) {
        // check if username or email already exists
        if (userService.findByUsername(user.getUsername()) != null || userService.findByEmail(user.getEmail()) != null) {
            bindingResult.rejectValue("username", "error.user", "User already exists");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("allRoles", roleService.getAll());
            return "admin/user/editUser";
        }

        user.setId(id);   // đảm bảo id không bị thay đổi
        Set<Role> roles = new HashSet<>();
        for (Integer roleId : roleIds) {
            Role role = roleService.getById(roleId);
            if (role != null) {
                roles.add(role);
            }
        }
        user.setRoles(roles);
        userService.save(user);
        return "redirect:/admin/user";
    }

    // DELETE
    @GetMapping("/delete")
    public String deleteUser(@RequestParam int id) {
        userService.delete(id);
        return "redirect:/admin/user";
    }
}