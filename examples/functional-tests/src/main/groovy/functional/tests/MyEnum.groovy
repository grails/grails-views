package functional.tests

/**
 * Created by Jim on 8/31/2016.
 */
enum MyEnum {

    FOO("Foo"),
    BAR("Bar")

    final String value

    MyEnum(String value) {
        this.value = value
    }

    String toString() {
        value
    }
}