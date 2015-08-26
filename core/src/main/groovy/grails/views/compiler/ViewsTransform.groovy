package grails.views.compiler

import grails.compiler.traits.TraitInjector
import grails.views.Views
import groovy.transform.CompilationUnitAware
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.DynamicVariable
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.grails.core.io.support.GrailsFactoriesLoader

import java.lang.reflect.Modifier

/**
 * Enhances view scripts with Trait behavior
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
@CompileStatic
class ViewsTransform implements ASTTransformation, CompilationUnitAware {
    CompilationUnit compilationUnit

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        def traitInjectors = GrailsFactoriesLoader.loadFactories(TraitInjector).findAll() { TraitInjector ti ->
            ti.artefactTypes.contains(Views.TYPE)
        }
        for(classNode in source.AST.classes) {
            if ( classNode.isScript() ) {
                for(injector in traitInjectors) {
                    classNode.addInterface(ClassHelper.make(injector.trait))
                }
                org.codehaus.groovy.transform.trait.TraitComposer.doExtendTraits(classNode, source, compilationUnit)

                new ModelTypesVisitor(source).visitClass(classNode)
            }
        }
    }

    class ModelTypesVisitor extends ClassCodeVisitorSupport {

        final SourceUnit sourceUnit
        ClassNode classNode
        Map<String, ClassNode> modelTypes = [:]

        ModelTypesVisitor(SourceUnit sourceUnit) {
            this.sourceUnit = sourceUnit
        }

        @Override
        void visitClass(ClassNode node) {
            this.classNode = node
            super.visitClass(node)
        }


        @Override
        void visitMethodCallExpression(MethodCallExpression call) {
            def methodName = call.getMethodAsString()
            def arguments = call.getArguments()

            if( methodName == "model" &&  (arguments instanceof ArgumentListExpression) ) {
                def args = ((ArgumentListExpression) arguments).getExpressions()
                if(args.size() == 1 ) {
                    def arg = args.get(0)
                    if(arg instanceof ClosureExpression) {
                        Statement body = ((ClosureExpression)arg).code
                        MapExpression map = new MapExpression()
                        if(body instanceof BlockStatement) {
                            for(Statement st in ((BlockStatement)body).getStatements()) {
                                if(st instanceof ExpressionStatement) {
                                    def expr = ((ExpressionStatement) st).expression
                                    if(expr instanceof DeclarationExpression) {
                                        VariableExpression var = (VariableExpression)((DeclarationExpression)expr).leftExpression
                                        classNode.addProperty(var.name, Modifier.PUBLIC, var.type.plainNodeReference, null, null, null)
                                        modelTypes[var.name] = var.type.plainNodeReference
                                        map.addMapEntryExpression(
                                                new MapEntryExpression(new ConstantExpression(var.name), new ClassExpression(var.type))
                                        )
                                    }
                                }
                            }
                        }
                        call.setMethod(new ConstantExpression("setModelTypes"))
                        call.setArguments(new ArgumentListExpression(map))
                    }
                }
            }
            classNode.putNodeMetaData(Views.MODEL_TYPES, modelTypes)
            super.visitMethodCallExpression(call)
        }
    }

}
