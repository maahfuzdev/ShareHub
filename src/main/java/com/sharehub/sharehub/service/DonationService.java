package com.sharehub.sharehub.service;

import com.sharehub.sharehub.dto.request.DonationRequest;
import com.sharehub.sharehub.entity.Donation;
import com.sharehub.sharehub.entity.DonationImage;
import com.sharehub.sharehub.entity.User;
import com.sharehub.sharehub.repository.DonationImageRepository;
import com.sharehub.sharehub.repository.DonationRepository;
import com.sharehub.sharehub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepository;
    private final DonationImageRepository donationImageRepository;
    private final UserRepository userRepository;

    private static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024;

    @Transactional(readOnly = true)
    public List<Donation> findAvailable(Donation.Origin origin) {
        return donationRepository.findByStatusAndOriginOrderByCreatedAtDesc(Donation.Status.AVAILABLE, origin);
    }

    @Transactional(readOnly = true)
    public List<Donation> findByDonor(String email) {
        return donationRepository.findByDonorEmailAndOriginOrderByCreatedAtDesc(email, Donation.Origin.DASHBOARD);
    }

    @Transactional(readOnly = true)
    public Optional<Donation> findOwnedById(Long id, String donorEmail) {
        return donationRepository.findByIdAndDonorEmail(id, donorEmail);
    }

    @Transactional
    public Donation create(DonationRequest request, String donorEmail) {
        return create(request, donorEmail, null);
    }

    @Transactional
    public Donation create(DonationRequest request, String donorEmail, MultipartFile image) {
        return create(request, donorEmail, image, Donation.Origin.DASHBOARD);
    }

    @Transactional
    public Donation createFromGeneralPage(DonationRequest request, String donorEmail, MultipartFile image) {
        return create(request, donorEmail, image, Donation.Origin.GENERAL);
    }

    private Donation create(
            DonationRequest request,
            String donorEmail,
            MultipartFile image,
            Donation.Origin origin
    ) {
        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated donor was not found"));

        Donation donation = new Donation();
        donation.setTitle(request.getTitle().trim());
        donation.setDescription(request.getDescription().trim());
        donation.setCategory(request.getCategory());
        donation.setQuantity(request.getQuantity().trim());
        donation.setLocation(request.getLocation().trim());
        donation.setAvailableUntil(request.getAvailableUntil());
        donation.setStatus(Donation.Status.AVAILABLE);
        donation.setOrigin(origin);
        donation.setDonor(donor);
        ImageContent imageContent = readImage(image);
        donation.setHasImage(imageContent != null);
        Donation savedDonation = donationRepository.save(donation);
        saveImage(savedDonation, imageContent);
        return savedDonation;
    }

    @Transactional
    public boolean update(Long id, DonationRequest request, String donorEmail) {
        return update(id, request, donorEmail, null);
    }

    @Transactional
    public boolean update(Long id, DonationRequest request, String donorEmail, MultipartFile image) {
        return donationRepository.findByIdAndDonorEmail(id, donorEmail)
                .map(donation -> {
                    donation.setTitle(request.getTitle().trim());
                    donation.setDescription(request.getDescription().trim());
                    donation.setCategory(request.getCategory());
                    donation.setQuantity(request.getQuantity().trim());
                    donation.setLocation(request.getLocation().trim());
                    donation.setAvailableUntil(request.getAvailableUntil());
                    ImageContent imageContent = readImage(image);
                    if (imageContent != null) {
                        donation.setHasImage(true);
                    }
                    donationRepository.save(donation);
                    saveImage(donation, imageContent);
                    return true;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Optional<DonationImage> findImageForUser(Long id, String email, User.Role role) {
        Optional<Donation> donation = role == User.Role.DONOR
                ? donationRepository.findByIdAndDonorEmail(id, email)
                : donationRepository.findByIdAndStatus(id, Donation.Status.AVAILABLE);

        return donation.filter(Donation::isHasImage)
                .flatMap(ownedDonation -> donationImageRepository.findById(ownedDonation.getId()));
    }

    @Transactional
    public boolean close(Long id, String donorEmail) {
        return setAvailability(id, donorEmail, false);
    }

    @Transactional
    public boolean setAvailability(Long id, String donorEmail, boolean available) {
        return donationRepository.findByIdAndDonorEmail(id, donorEmail)
                .map(donation -> {
                    donation.setStatus(available ? Donation.Status.AVAILABLE : Donation.Status.CLOSED);
                    donationRepository.save(donation);
                    return true;
                })
                .orElse(false);
    }

    private ImageContent readImage(MultipartFile image) {
        if (image == null || image.isEmpty()) return null;
        if (image.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new IllegalArgumentException("Image must be 5 MB or smaller.");
        }

        try {
            byte[] data = image.getBytes();
            if (data.length > MAX_IMAGE_SIZE_BYTES) {
                throw new IllegalArgumentException("Image must be 5 MB or smaller.");
            }
            String contentType = detectImageContentType(data);
            return new ImageContent(data, contentType);
        } catch (IOException exception) {
            throw new IllegalArgumentException("The selected image could not be read.", exception);
        }
    }

    private String detectImageContentType(byte[] data) {
        if (data.length >= 3
                && (data[0] & 0xff) == 0xff
                && (data[1] & 0xff) == 0xd8
                && (data[2] & 0xff) == 0xff) {
            return "image/jpeg";
        }
        if (data.length >= 8
                && (data[0] & 0xff) == 0x89
                && data[1] == 0x50 && data[2] == 0x4e && data[3] == 0x47
                && data[4] == 0x0d && data[5] == 0x0a && data[6] == 0x1a && data[7] == 0x0a) {
            return "image/png";
        }
        if (data.length >= 12
                && data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F'
                && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P') {
            return "image/webp";
        }
        throw new IllegalArgumentException("Choose a valid JPEG, PNG, or WebP image.");
    }

    private void saveImage(Donation donation, ImageContent imageContent) {
        if (imageContent == null) return;

        DonationImage donationImage = donationImageRepository.findById(donation.getId())
                .orElseGet(DonationImage::new);
        donationImage.setDonation(donation);
        donationImage.setImageData(imageContent.data());
        donationImage.setContentType(imageContent.contentType());
        donationImageRepository.save(donationImage);
    }

    private record ImageContent(byte[] data, String contentType) { }
}
