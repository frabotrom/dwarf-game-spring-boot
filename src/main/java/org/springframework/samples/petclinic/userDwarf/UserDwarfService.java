package org.springframework.samples.petclinic.userDwarf;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.user.AuthoritiesService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserDwarfService {

    @Autowired
	private AuthoritiesService authoritiesService;

    @Autowired
    private UserDwarfRepository userDwarfRepository;
    
    @Transactional
    public int userDwarfCount(){
        return (int) userDwarfRepository.count();
    }
    @Transactional
    public Iterable<UserDwarf> findAll(){
        return userDwarfRepository.findAll();

    }

    @Transactional(readOnly = true)
	public UserDwarf findById(int id) throws DataAccessException {
		return userDwarfRepository.findById(id);
	}

    @Transactional
	public void saveUserDwarf(UserDwarf userDwarf, List<String> roles) throws DataAccessException {
        // Saving user to repository
        userDwarfRepository.save(userDwarf);
        
        // Saving authorities
        roles.stream().forEach(role->authoritiesService.saveAuthorities(userDwarf.getUsername(), role));
        
	}

    @Transactional(readOnly = true)
	public Collection<UserDwarf> findUserDwarfByUsername(String username) throws DataAccessException {
		return userDwarfRepository.findByUsername(username);
	}

    public Optional<UserDwarf> findUserDwarfByUsername2(String username) {
		return userDwarfRepository.findByUsername2(username);
	}
}
