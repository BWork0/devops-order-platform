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

  config.vm.define "k3s-server" do |machine|
    machine.vm.hostname = "k3s-server"
    machine.vm.network "private_network", ip: "10.10.10.101"
    machine.vm.provider "virtualbox" do |vb|
      vb.memory = 4096
      vb.cpus = 2
    end
  end

  config.vm.define "k3s-worker-1" do |machine|
    machine.vm.hostname = "k3s-worker-1"
    machine.vm.network "private_network", ip: "10.10.10.102"
    machine.vm.provider "virtualbox" do |vb|
      vb.memory = 4096
      vb.cpus = 2
    end
  end

  config.vm.define "k3s-worker-2" do |machine|
    machine.vm.hostname = "k3s-worker-2"
    machine.vm.network "private_network", ip: "10.10.10.103"
    machine.vm.provider "virtualbox" do |vb|
      vb.memory = 4096
      vb.cpus = 2
    end
  end
end