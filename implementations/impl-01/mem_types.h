
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#pragma once

struct mem_section {
  struct ph_entry ph;
  void *mem;
  struct mem_section *next;
};
