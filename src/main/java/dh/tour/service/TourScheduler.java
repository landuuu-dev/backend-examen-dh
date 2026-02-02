package dh.tour.service;

import dh.tour.model.Tour;
import dh.tour.model.EstadoTour; // Importa el Enum
import dh.tour.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TourScheduler {

    private final TourRepository tourRepository;

    @Scheduled(cron = "0 0 0 * * ?") // Se ejecuta cada medianoche
    public void actualizarToursVencidos() {
        List<Tour> tours = tourRepository.findAll();
        for (Tour tour : tours) {
            // CAMBIO: Ahora comparamos con el Enum EstadoTour.ACTIVO
            if (tour.getEstado() == EstadoTour.ACTIVO && LocalDate.now().isAfter(tour.getFechaFin())) {

                // CAMBIO: Ahora asignamos el estado CANCELADO (o AGOTADO según prefieras)
                tour.setEstado(EstadoTour.CANCELADO);
                tourRepository.save(tour);

                System.out.println("🤖 Scheduler: Tour [" + tour.getNombre() + "] marcado como CANCELADO por fecha vencida.");
            }
        }
    }
}