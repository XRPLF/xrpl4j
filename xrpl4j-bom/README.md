# XRPL-4j Bill of Materials (BOM)

A BOM is a special kind of Maven POM file that allows downstream projects using xrpl-4j to control dependency versions
in one place.

## Usage

To use this bom in your project, add the following to the `<dependencyManamgement/>` section of your project's primary
POM file. For example:

```xml
<project ...>
  <groupId>...</groupId>
  <artifactId>...</artifactId>
  <version>...</version>
  <packaging>...</packaging>
    ...
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.xrpl.xrpl4j</groupId>
        <artifactId>xrpl4j-bom</artifactId>
        <version>3.0.0</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement> 
</project>
```

With this in place, whenever you want to add a `<dependency/>` you won't need to worry about specifying the version.
Instead, version numbers are controlled by the BOM you import, as in the example above, which will use only
version `3.0.0` of all xrpl-4j dependencies.

For more information on how BOM files work, consult this [tutorial](https://www.baeldung.com/spring-maven-bom) or others
on Google.
