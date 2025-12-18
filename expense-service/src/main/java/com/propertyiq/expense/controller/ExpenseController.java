package com.propertyiq.expense.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/properties/{propertyId}/expenses")
public class ExpenseController {

    @GetMapping
    public String getExpenses(@PathVariable String propertyId,
                              @RequestParam(required = false) String from,
                              @RequestParam(required = false) String to,
                              @RequestParam(required = false) String category) {
        return "Expense Service - Get expenses for property: " + propertyId;
    }

    @PostMapping
    public String createExpense(@PathVariable String propertyId) {
        return "Expense Service - Create expense for property: " + propertyId;
    }

    @PutMapping("/{expenseId}")
    public String updateExpense(@PathVariable String propertyId, @PathVariable String expenseId) {
        return "Expense Service - Update expense: " + expenseId;
    }

    @DeleteMapping("/{expenseId}")
    public String deleteExpense(@PathVariable String propertyId, @PathVariable String expenseId) {
        return "Expense Service - Delete expense: " + expenseId;
    }
}
