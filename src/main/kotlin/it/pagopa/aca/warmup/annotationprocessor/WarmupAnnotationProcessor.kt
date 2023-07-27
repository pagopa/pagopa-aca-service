package it.pagopa.aca.warmup.annotationprocessor

import it.pagopa.aca.warmup.annotations.WarmupFunction
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.lang.model.element.*
import javax.tools.Diagnostic
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController

@SupportedAnnotationTypes("it.pagopa.aca.WarmupFunction")
class WarmupAnnotationProcessor : AbstractProcessor() {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        logger.info("WarmupAnnotationProcessor initialized")
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        logger.info("WarmupAnnotationProcessor start process")
        for (element: Element in roundEnv?.getElementsAnnotatedWith(WarmupFunction::class.java)!!) {
            if (element is ExecutableElement) {
                val declaringClass: Element = element.getEnclosingElement()
                val modifiers: Set<Modifier> = element.getModifiers()
                val className = declaringClass.toString()
                val warmupMethod: String = element.getSimpleName().toString()
                val parameters: List<VariableElement?> = element.parameters

                if (parameters.isNotEmpty()) {
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Warmup method: [%s.%s] should not have arguments".format(
                            className,
                            warmupMethod
                        )
                    )
                }
                if (modifiers.size != 1 || !modifiers.contains(Modifier.PUBLIC)) {
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Warmup method: [%s.%s] should have only public modifier".format(
                            className,
                            warmupMethod
                        )
                    )
                }
                if (declaringClass.getAnnotation(RestController::class.java) == null) {
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Found warmup method in class [%s] but is not annotated with @RestController".format(
                            className
                        )
                    )
                }
            } else {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Invalid annotation location, annotation expected on method but found on: [%s]".format(
                        element.kind
                    )
                )
            }
        }

        return true
    }
}
