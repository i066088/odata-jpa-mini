# odata-jpa-mini

Best 60-70% of OData protocol, for Java EE/JPA applications.
 
Use case:
* You want to serve some JPA data via REST/OData protocol.
* You are in a Java EE 7+ context, so you can use JAX-RS and CDI
* You don't really need to be 100% OData compliant

Currently, the only way to do this is to use Olingo [1] with Olingo JPA Processor [2], with the following possible problems:
(1) this means to load a large number of libraries, (2) Olingo JPA Processor is not stable yet for production, (3) Olingo JPA Processor requires Java 8.

**Warning: requires Jackson as serializer, MOXy won't work**


## Limitations of this implementation
* Root and metadata documents are not implemented
* Navigation is not implemented
* Several query options are not implemented, such as $search and $expand
* Entity id's must all be Long! This is quite bad but I don't know how to solve it yet.
* No OData metadata (is this really a limitation?)
* No ETag support

See more details within the example webapp: https://github.com/luca-vercelli/odata-jpa-mini/blob/master/example-webapp/src/main/webapp/index.html

## Comparison between OData and JPA

JPA is far less powerful than OData.

| OData	        |  Java          |
| ------------- | -------------- |
| Primitive  Type |  Integer, Boolean, ... see [3] |
| Enum Type    |   Enum        |
| Entity Type   |  @Entity class |
| Complex Type    | @Embedded class |


**There are no Entity Sets, nor Singletons (at configuration level).**
In JPA all you can define is the Entity Type.
An entity set is a group of individuals of the same type, and a singleton is a single individual of that type.
To define them in JPA you need a method that returns them. 
We assume one only entity set per each type, which is the full set of all individuals. 
(The most similar concept to an entity set in JPA is a NamedQuery without binding variables).

**There are no Actions nor Functions (at configuration level).**
To define them you should write a method that compute them. 
The distinction between Actions and Functions is purely semantical: a function must not modify the database, while an action can (and should) do it. 

**No distinction between (relation) Properties and Navigation Properties.**
This distinction is purely semantical: a navigation property is a relation property towards entity/ies *that are not part of the object itself*. 
We assume that all relations (i.e. properties that return either an entity or a collection of entities) are navigation properties.
(One may also assume that a navigation property is a non-EAGER relation).

**Limited support for multi-column keys.**
Each entity in JPA must have one and only one key attribute. 
This attribute may be an Embedded object, thus simulating a multi-column key.


----------------------------------------------------

[1] https://github.com/apache/olingo-odata4

[2] https://github.com/SAP/olingo-jpa-processor-v4

[3] https://olingo.apache.org/javadoc/odata4/org/apache/olingo/commons/api/edm/EdmPrimitiveType.html