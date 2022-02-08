package com.myorg;

import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.CfnParameter;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.amazonmq.CfnBroker;
import software.amazon.awscdk.services.amazonmq.CfnBroker.UserProperty;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.AddCapacityOptions;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.Secret;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedEc2Service;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.rds.DatabaseInstance;
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine;
import software.amazon.awscdk.services.rds.DatabaseSecret;
import software.amazon.awscdk.services.rds.PostgresEngineVersion;
import software.amazon.awscdk.services.rds.PostgresInstanceEngineProps;
import software.amazon.awscdk.services.rds.StorageType;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketAccessControl;
import software.amazon.awscdk.services.secretsmanager.ISecret;
import software.amazon.awscdk.services.ssm.StringParameter;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

public class HelloSdkStack extends Stack {
    public HelloSdkStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public HelloSdkStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        //Parameters
        CfnParameter ecrRepositoryName = CfnParameter.Builder.create(this, "ecrRepositoryName")
                .description("Name of repository ECR").build();

        CfnParameter imageTag = CfnParameter.Builder.create(this, "imageTag")
                .description("Tag of image").build();

        CfnParameter postgresUsername = CfnParameter.Builder.create(this, "postgresUsername")
                .defaultValue("postgres")
                .build();

        CfnParameter brokerUsername = CfnParameter.Builder.create(this, "brokerUsername")
                .defaultValue("user")
                .build();

        // S3 bucket resources
        Bucket bucket = Bucket.Builder.create(this, "MyBucket")
                .bucketName("admin.goods.images")
                .accessControl(BucketAccessControl.PUBLIC_READ)
                .publicReadAccess(true)
                .versioned(false)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();


        // VPC
        Vpc vpc = Vpc.Builder.create(this, "VPC")
                .vpcName("MyVpc")
                .subnetConfiguration(Vpc.DEFAULT_SUBNETS_NO_NAT)
                .maxAzs(3)
                .build();

        //Postgres Database resources
        DatabaseSecret databaseSecret = DatabaseSecret.Builder.create(this, "postgresSecret")
                .username(postgresUsername.getValueAsString())
                .build();

        DatabaseInstance postgres = DatabaseInstance.Builder.create(this, "Postgres")
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PUBLIC).build())
                .engine(
                        DatabaseInstanceEngine.postgres(
                                PostgresInstanceEngineProps.builder()
                                        .version(PostgresEngineVersion.VER_12_8)
                                        .build()
                        )
                )
                .credentials(Credentials.fromSecret(databaseSecret))
                .databaseName("adminDatabase")
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
                .storageType(StorageType.GP2)
                .publiclyAccessible(true)
                .iamAuthentication(false)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        postgres.getConnections().allowFromAnyIpv4(Port.tcp(5432), "Allow all connections to the port 5432");


        // ElasticSearch resources
        Cluster cluster = Cluster.Builder.create(this, "shopping-cluster")
                .vpc(vpc)
                .capacity(
                        AddCapacityOptions.builder()
                                .desiredCapacity(1)
                                .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MICRO))
                                .associatePublicIpAddress(true)
                                .vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PUBLIC).build())
                                .build())
                .build();


        // MessageBroker
        UserProperty userProperty = new UserProperty() {
            @Override
            public @NotNull String getPassword() {
                return "password12345";
            }

            @Override
            public @NotNull String getUsername() {
                return brokerUsername.getValueAsString();
            }
        };

        CfnBroker broker = CfnBroker.Builder.create(this, "shopping-broker")
                .brokerName("shopping-application-broker")
                .deploymentMode("SINGLE_INSTANCE")
                .engineType("RABBITMQ")
                .engineVersion("3.8.26")
                .hostInstanceType("mq.t3.micro")
                .publiclyAccessible(true)
                .users(List.of(userProperty))
                .autoMinorVersionUpgrade(false)
                .build();

        StringParameter brokerEndpointSSM = StringParameter.Builder.create(this, "brokerEndpointSSM")
                .parameterName("brokersEndpointSSM")
                .stringValue(Fn.select(0, broker.getAttrAmqpEndpoints()))
                .build();

        StringParameter brokerUsernameSSM = StringParameter.Builder.create(this, "brokerUsernameSSM")
                .parameterName("brokersUsernameSSM")
                .stringValue(userProperty.getUsername())
                .build();

        StringParameter brokerPasswordSSM = StringParameter.Builder.create(this, "brokerPasswordSSM")
                .parameterName("brokersPasswordSSM")
                .stringValue(userProperty.getPassword())
                .build();


        //EC2 service
        IRole serviceTaskRole = Role.Builder.create(this, "ServiceTaskRole")
                .assumedBy(ServicePrincipal.Builder.create("ecs-tasks.amazonaws.com").build()).build();

        ISecret secret = software.amazon.awscdk.services.secretsmanager.Secret.fromSecretCompleteArn(
                this, "awsSecrets", "arn:aws:secretsmanager:eu-west-2:356106348453:secret:aws_secrets-U7vJ29");

        ApplicationLoadBalancedEc2Service ec2Service = ApplicationLoadBalancedEc2Service.Builder.create(this, "shopping-service")
                .cluster(cluster)
                .publicLoadBalancer(true)
                .healthCheckGracePeriod(Duration.hours(4))
                .desiredCount(1)
                .cpu(512)
                .memoryLimitMiB(700)
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .containerName("shopping")
                                .taskRole(serviceTaskRole)
                                .environment(Map.of(
                                        "POSTGRES_USER", postgresUsername.getValueAsString(),
                                        "POSTGRES_HOST",postgres.getDbInstanceEndpointAddress(),
                                        "POSTGRES_PORT",postgres.getDbInstanceEndpointPort(),
                                        "S3_REGION", getRegion(),
                                        "S3_BUCKET_NAME", bucket.getBucketName(),
                                        "RABBIT_ENDPOINT", Fn.select(0, broker.getAttrAmqpEndpoints()),
                                        "RABBIT_USERNAME", userProperty.getUsername(),
                                        "RABBIT_PASSWORD", userProperty.getPassword()
                                ))
                                .secrets(Map.of(
                                        //TO DELETE
                                        "aws.access.key", Secret.fromSecretsManager(secret, "ACCESS_KEY"),
                                        "aws.secret.key", Secret.fromSecretsManager(secret, "SECRET_KEY"),
                                        "POSTGRES_PASSWORD", Secret.fromSecretsManager(databaseSecret, "password")
                                ))
                                .image(
                                        ContainerImage.fromEcrRepository(
                                                Repository.fromRepositoryName(this, "EcrRepository", ecrRepositoryName.getValueAsString()),
                                                imageTag.getValueAsString()
                                        )
                                )
                                .containerPort(8081)
                                .build()
                )
                .build();

        StringParameter.Builder.create(this, "loadBalancerDns")
                .parameterName("adminServiceLoadBalancerDNSName")
                .stringValue(ec2Service.getLoadBalancer().getLoadBalancerDnsName())
                .build();

    }
}
