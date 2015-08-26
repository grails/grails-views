# Grails Views

Additional View Technologies for Grails

Initial implementation includes JSON views powered by Groovy's JsonBuilder.

## JSON Views

JSON views go into the `grails-app/views` directory and end with the `.gson` suffix. They are regular Groovy scripts and can be opened in any Groovy editor.

Example JSON view:

    json.person {
        name "bob"
    }
    
Produces

    {"person":{ "name": "bob"}}
