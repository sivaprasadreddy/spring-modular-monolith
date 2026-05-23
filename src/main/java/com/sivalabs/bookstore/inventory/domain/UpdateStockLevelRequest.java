package com.sivalabs.bookstore.inventory.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

record UpdateStockLevelRequest(@NotNull @Min(0) Long quantity) {}
