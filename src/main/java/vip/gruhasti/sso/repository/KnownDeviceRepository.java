package vip.gruhasti.sso.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vip.gruhasti.sso.model.KnownDevice;

import java.util.Optional;

public interface KnownDeviceRepository extends MongoRepository<KnownDevice, String> {
    Optional<KnownDevice> findByUserIdAndDeviceHash(String userId, String deviceHash);
}
