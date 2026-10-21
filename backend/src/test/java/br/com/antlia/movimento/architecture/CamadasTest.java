package br.com.antlia.movimento.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
class CamadasTest {
 @Test void dominioEAplicacaoIndependentesDeFrameworks() {
  var classes=new ClassFileImporter().importPackages("br.com.antlia.movimento");
  noClasses().that().resideInAnyPackage("..domain..","..application..")
   .should().dependOnClassesThat().resideInAnyPackage("org.springframework..","jakarta.persistence..","..infrastructure..","..presentation..").check(classes);
  noClasses().that().resideInAPackage("..presentation..").should().dependOnClassesThat().resideInAPackage("..persistence..").check(classes);
 }
}
