package functional.tests.api

import functional.tests.Book


class BookController {

    static namespace = 'api'

    static responseFormats = ['json']

    def index() {
        respond new Book(title: 'API - The Shining')
    }

    def nested() {
        respond new Book(title: 'API - The Shining')
    }
}