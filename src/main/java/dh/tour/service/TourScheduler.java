package dh.tour.service;

import dh.tour.model.Tour;
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
            if (tour.isDisponible() && LocalDate.now().isAfter(tour.getFechaFin())) {
                tour.setDisponible(false);
                tourRepository.save(tour);
            }
        }
    }
}