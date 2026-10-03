Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/jammy64"
  config.vm.define "cicd" do |machine|
    machine.vm.hostname = "cicd"
    machine.vm.network "private_network", ip: "10.10.10.100"
    machine.vm.provider "virtualbox" do |vb|
      vb.memory = 8192
      vb.cpus = 4
    end
  end

  config.vm.define "ci-agent" do |machine|
    machine.vm.hostname = "ci-agent"
    machine.vm.network "private_network", ip: "10.10.10.104"
    machine.vm.provider "virtualbox" do |vb|
      vb.memory = 4096
      vb.cpus = 2
    end
  end

end