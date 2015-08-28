package grails.plugin.json.view.internal

import grails.plugin.json.builder.StreamingJsonBuilder
import grails.views.compiler.BuilderTypeCheckingExtension
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode

/**
 * A type checking extension for JSON builder
 *
 * @author Graeme Rocher
 */
class JsonTemplateTypeCheckingExtension extends BuilderTypeCheckingExtension {
    private static final ClassNode BUILDER_CLASS_NODE = ClassHelper.make(StreamingJsonBuilder)
    private static final MethodNode JSON_BUILDER_INVOKE_METHOD = ClassHelper.make(StreamingJsonBuilder).getMethods('invokeMethod')[0]
    private static final MethodNode JSON_DELEGATE_INVOKE_METHOD = ClassHelper.make(StreamingJsonBuilder.StreamingJsonDelegate).getMethods('invokeMethod')[0]


    @Override
    MethodNode getBuilderInvokeMethod() {
        JSON_BUILDER_INVOKE_METHOD
    }

    @Override
    MethodNode getDelegateInvokeMethod() {
        JSON_DELEGATE_INVOKE_METHOD
    }

    @Override
    String getBuilderVariableName() {
        "json"
    }

    @Override
    ClassNode getBuilderClassNode() {
        return BUILDER_CLASS_NODE
    }
}
