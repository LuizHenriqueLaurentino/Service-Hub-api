package com.servicehub.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InputSanitizerTest {

    @Test
    @DisplayName("sanitize: remove bloco <script> inteiro, inclusive o conteúdo")
    void removesScriptBlock() {
        assertEquals("Limpeza", InputSanitizer.sanitize("<script>alert(1)</script>Limpeza"));
    }

    @Test
    @DisplayName("sanitize: remove tags HTML mantendo o texto")
    void removesHtmlTags() {
        assertEquals("Pintura de parede", InputSanitizer.sanitize("<b>Pintura</b> de <i>parede</i>"));
    }

    @Test
    @DisplayName("sanitize: remove atributos de evento como onerror")
    void removesEventHandlerTags() {
        assertEquals("Foto", InputSanitizer.sanitize("<img src=x onerror=alert(1)>Foto"));
    }

    @Test
    @DisplayName("sanitize: aplica trim e remove caracteres de controle")
    void trimsAndRemovesControlChars() {
        assertEquals("Texto", InputSanitizer.sanitize("  Te\u0000xto  "));
    }

    @Test
    @DisplayName("sanitize: texto normal com acentos permanece igual")
    void keepsPlainText() {
        assertEquals("Instalação de chuveiro elétrico", InputSanitizer.sanitize("Instalação de chuveiro elétrico"));
    }

    @Test
    @DisplayName("sanitize: null devolve null")
    void nullStaysNull() {
        assertNull(InputSanitizer.sanitize(null));
    }
}
