class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')

        "/books"(resources:"book")
        "/teams"(resources:"team")
        "/teams/deep/$id"(controller: "team", action:"deep")
        "/authors"(resources:"author")
    }
}
