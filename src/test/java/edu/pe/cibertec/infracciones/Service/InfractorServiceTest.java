package edu.pe.cibertec.infracciones.Service;

import edu.pe.cibertec.infracciones.model.EstadoMulta;
import edu.pe.cibertec.infracciones.model.Infractor;
import edu.pe.cibertec.infracciones.model.Multa;
import edu.pe.cibertec.infracciones.model.Vehiculo;
import edu.pe.cibertec.infracciones.repository.InfractorRepository;
import edu.pe.cibertec.infracciones.repository.MultaRepository;
import edu.pe.cibertec.infracciones.repository.VehiculoRepository;
import edu.pe.cibertec.infracciones.service.impl.InfractorServiceImpl;
import edu.pe.cibertec.infracciones.service.impl.MultaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InfractorServiceTest {

    @Mock
    private MultaRepository multaRepository;

    @Mock
    private InfractorRepository infractorRepository;

    @Mock
    private VehiculoRepository vehiculoRepository;

    @InjectMocks
    private InfractorServiceImpl infractorService;

    @InjectMocks
    private MultaServiceImpl multaService;

    @Test
    void calcularDeudaTest() {

        Long id = 1L;

        Multa m1 = new Multa();
        m1.setMonto(200.0);
        m1.setEstado(EstadoMulta.PENDIENTE);

        Multa m2 = new Multa();
        m2.setMonto(300.0);
        m2.setEstado(EstadoMulta.VENCIDA);

        when(multaRepository.findByInfractorId(id))
                .thenReturn(Arrays.asList(m1, m2));

        double resultado = infractorService.calcularDeuda(id);

        assertEquals(545.0, resultado);
    }

    @Test
    void desasignarVehiculo_sinMultasPendientes_deberiaEliminarVehiculo() {

        Long infractorId = 1L;
        Long vehiculoId = 1L;

        Infractor infractor = new Infractor();
        infractor.setId(infractorId);
        infractor.setVehiculos(new ArrayList<>());

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(vehiculoId);

        infractor.getVehiculos().add(vehiculo);

        // no hay multas pendientes
        when(infractorRepository.findById(infractorId))
                .thenReturn(Optional.of(infractor));

        when(vehiculoRepository.findById(vehiculoId))
                .thenReturn(Optional.of(vehiculo));

        when(multaRepository.findByInfractorId(infractorId))
                .thenReturn(new ArrayList<>());

        infractorService.desasignarVehiculo(infractorId, vehiculoId);

        assertFalse(infractor.getVehiculos().contains(vehiculo));

        verify(infractorRepository).save(infractor);
    }
    @Test
    void transferirMulta_correctamente() {

        Long multaId = 1L;
        Long infractorId = 2L;

        Multa multa = new Multa();
        multa.setId(multaId);
        multa.setEstado(EstadoMulta.PENDIENTE);

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(10L);
        multa.setVehiculo(vehiculo);

        Infractor nuevoInfractor = new Infractor();
        nuevoInfractor.setId(infractorId);
        nuevoInfractor.setBloqueado(false);
        nuevoInfractor.setVehiculos(new ArrayList<>());
        nuevoInfractor.getVehiculos().add(vehiculo);

        when(multaRepository.findById(multaId))
                .thenReturn(Optional.of(multa));

        when(infractorRepository.findById(infractorId))
                .thenReturn(Optional.of(nuevoInfractor));

        multaService.transferirMulta(multaId, infractorId);

        assertEquals(nuevoInfractor, multa.getInfractor());

        verify(multaRepository).save(multa);
    }
    @Test
    void transferirMulta_infractorBloqueado_noDebeGuardar() {

        Long multaId = 1L;
        Long infractorId = 2L;

        Multa multa = new Multa();
        multa.setId(multaId);
        multa.setEstado(EstadoMulta.PENDIENTE);

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(10L);
        multa.setVehiculo(vehiculo);

        Infractor infractorBloqueado = new Infractor();
        infractorBloqueado.setId(infractorId);
        infractorBloqueado.setBloqueado(true);

        when(multaRepository.findById(multaId))
                .thenReturn(Optional.of(multa));

        when(infractorRepository.findById(infractorId))
                .thenReturn(Optional.of(infractorBloqueado));

        assertThrows(RuntimeException.class, () -> {
            multaService.transferirMulta(multaId, infractorId);
        });

        verify(multaRepository, never()).save(any());
    }
    @Test
    void transferirMulta_argumentCaptor() {

        Long multaId = 1L;
        Long infractorId = 2L;

        Multa multa = new Multa();
        multa.setId(multaId);
        multa.setEstado(EstadoMulta.PENDIENTE);

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(10L);
        multa.setVehiculo(vehiculo);

        Infractor nuevoInfractor = new Infractor();
        nuevoInfractor.setId(infractorId);
        nuevoInfractor.setBloqueado(false);
        nuevoInfractor.setVehiculos(new ArrayList<>());
        nuevoInfractor.getVehiculos().add(vehiculo);

        when(multaRepository.findById(multaId))
                .thenReturn(Optional.of(multa));

        when(infractorRepository.findById(infractorId))
                .thenReturn(Optional.of(nuevoInfractor));

        multaService.transferirMulta(multaId, infractorId);

        ArgumentCaptor<Multa> captor = ArgumentCaptor.forClass(Multa.class);

        verify(multaRepository).save(captor.capture());

        Multa multaGuardada = captor.getValue();

        assertEquals(nuevoInfractor, multaGuardada.getInfractor());
    }
}