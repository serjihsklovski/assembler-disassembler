# Assembler, Disassembler

## API

### Assemble:

```
ad -assemble -input <input_file_path> -output <output_file_path>
```

### Disassemble:

```
ad -disassemble -input <input_file_path> -output <output_file_path>
```

## Supported ASM X86 Instructions

* `mov <reg>, <reg>`
* `mov <reg>, <num>`
* `add <reg>, <reg>`
* `add <reg>, <num>`
* `not <reg>`
* `shr <reg>`
* `shr <reg>, <num>`
* `jmp <num>`
