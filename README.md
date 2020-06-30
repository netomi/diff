diff
====

This is a fast library for file comparison in Java. It provides an implementation of the
comparison algorithm described in *An O(ND) Difference Algorithm and its Variations* by Eugene W. Myers.

## Supported output formats

* Classic diff
* Edit script
* Unified format
* Side-by-Side output

## Example

Here is a quick example:

```java
public class Test {

    public static void main(String[] args) {
        Patch patch = DiffUtils.diff(args[0], args[1]);
        new ClassicDiffFormatter().format(patch, System.out);
    } 

}
```
