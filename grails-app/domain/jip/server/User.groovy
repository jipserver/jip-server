package jip.server

class User {

	transient springSecurityService

    String id
	String username
	String password
	String email
	boolean enabled
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired
    Map<String, Long> clusterUser

	static constraints = {
		username blank: false, unique: true
		password blank: false
        email blank: true, nullable: true
	}

	static mapping = {
		password column: '`password`'
	}

	Set<Role> getAuthorities() {
		UserRole.findAllByUser(this).collect { it.role } as Set
	}

	public void encodePassword() {
		password = springSecurityService.encodePassword(password)
	}

    def hasRole(String role) {
        return authorities.any{it.authority == role}
    }
    /**
     * Return a ClusterUser for the given cluster or null
     *
     * @param cluster the cluster
     * @return clusterUser the cluster user associated with this account
     */
    ClusterUser getRemoteUser(Cluster cluster){
        if(!cluster) return null
        if(clusterUser == null) return null
        def uid = clusterUser.get(cluster.getName())
        if(uid){
            return ClusterUser.get(uid)
        }
        return null
    }
}
