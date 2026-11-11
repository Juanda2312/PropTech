package co.edu.uniquindio.poo.PropTech.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "proptech.data-dir=${java.io.tmpdir}/proptech-test/sin-slash-final")
class PersistenciaServiceDirTest {

    static final Path DIR = Path.of(System.getProperty("java.io.tmpdir"), "proptech-test", "sin-slash-final");

    @BeforeAll
    static void limpiar() throws IOException {
        if (Files.exists(DIR)) FileSystemUtils.deleteRecursively(DIR);
    }

    @Test
    void creaLaCarpetaConfigurada_aunqueNoTerminaEnSlash() {
        assertTrue(Files.isDirectory(DIR));
    }
}