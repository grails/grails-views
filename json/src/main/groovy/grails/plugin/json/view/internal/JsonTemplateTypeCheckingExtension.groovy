package grails.plugin.json.view.internal

import grails.plugin.json.builder.StreamingJsonBuilder
import grails.plugin.json.view.api.internal.TemplateNamespaceInvoker
import grails.views.compiler.BuilderTypeCheckingExtension
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.control.SourceUnit

/**
 * A type checking extension for JSON builder
 *
 * @author Graeme Rocher
 */
class JsonTemplateTypeCheckingExtension extends BuilderTypeCheckingExtension {
    private static final ClassNode BUILDER_CLASS_NODE = ClassHelper.make(StreamingJsonBuilder)
    private static final MethodNode JSON_BUILDER_INVOKE_METHOD = ClassHelper.make(StreamingJsonBuilder).getMethods('invokeMethod')[0]
    private static final MethodNode JSON_DELEGATE_INVOKE_METHOD = ClassHelper.make(StreamingJsonBuilder.StreamingJsonDelegate).getMethods('invokeMethod')[0]
    private static final ClassNode TEMPLATE_NAMESPACE = ClassHelper.make(TemplateNamespaceInvoker)
    private static final MethodNode TEMPLATE_NAMESPACE_INVOKE_METHOD = ClassHelper.make(TemplateNamespaceInvoker).getMethods('invokeMethod')[0]

    @Override
    boolean isMethodDynamic(Object receiver, Object name, Object argList, Object argTypes, Object call) {
        if( receiver.name == TEMPLATE_NAMESPACE.name) {
            return true
        }
        return super.isMethodDynamic(receiver, name, argList, argTypes, call)
    }

    @Override
    void transformDynamicMethods(SourceUnit source, MethodNode mn, Set dynamicCalls) {
        new BuilderTypeCheckingExtension.BuilderMethodReplacer(TEMPLATE_NAMESPACE_INVOKE_METHOD, TEMPLATE_NAMESPACE_INVOKE_METHOD, ":IGNORE", source, dynamicCalls)
                .visitMethod(mn)
    }

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
