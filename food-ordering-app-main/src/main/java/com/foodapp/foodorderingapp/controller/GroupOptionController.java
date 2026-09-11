package com.foodapp.foodorderingapp.controller;

import com.foodapp.foodorderingapp.dto.group_option.GroupOptionRequest;
import com.foodapp.foodorderingapp.entity.GroupOption;
import com.foodapp.foodorderingapp.security.UserPrinciple;
import com.foodapp.foodorderingapp.service.group_option.GroupOptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group_options")
@RequiredArgsConstructor
public class GroupOptionController {
    private final GroupOptionService groupOptionService;
    @GetMapping("list/{id}")
    public ResponseEntity<List<GroupOption>> getAllGroupOptions(@PathVariable long id){
        return ResponseEntity.ok(groupOptionService.getGroupOptionsByRestaurantId(id));
    }
    @GetMapping("dish/{id}")
    public ResponseEntity<List<GroupOption>> getGroupOptionOfDish(@PathVariable("id") long dishId){
        return ResponseEntity.ok(groupOptionService.getGroupOptionsOfDish(dishId));
    }
    @GetMapping("restaurant/{id}")
    public ResponseEntity<List<GroupOption>> getGroupOptionOfRestaurant(@PathVariable("id") long resId){
        return ResponseEntity.ok(groupOptionService.getGroupOptionsByRestaurantId(resId));
    }
    @PostMapping
    public ResponseEntity<GroupOption> createGroupOption(@Valid @RequestBody GroupOptionRequest groupOptionRequest) {
        return ResponseEntity.ok(groupOptionService.addGroupOption(groupOptionRequest));
    }
    @PutMapping("/{id}")
    public ResponseEntity<GroupOption> updateGroupOption(@Valid @RequestBody GroupOptionRequest groupOptionRequest, @PathVariable("id") long groupOptionId) throws Exception {
        return ResponseEntity.ok(groupOptionService.updateGroupOption(groupOptionId, groupOptionRequest));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteGroupOption(@PathVariable("id") long groupOptionId) throws Exception {
        groupOptionService.deleteGroupOption(groupOptionId);
        return ResponseEntity.ok(true);
    }

    private void checkUserPermission(long userId){
        UserPrinciple userInfo = (UserPrinciple) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(userInfo.getUserId() !=  userId){
            throw new IllegalArgumentException("User doesn't have permission");
        }
    }
}
